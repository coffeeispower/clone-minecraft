import {
	createSignal,
	createEffect,
	tween,
	easeInOutCubic,
	waitFor,
	beginSlide,
	all,
	createRef,
	Vector2,
	Reference,
	SimpleSignal,
} from "@motion-canvas/core";
import { Img, makeScene2D, Rect, Txt, View2D } from "@motion-canvas/2d";
import projectionMatrix from "../assets/projectionmatrix.png";
import {
	Scene,
	PerspectiveCamera,
	OrthographicCamera,
	Mesh,
	MeshBasicMaterial,
	BoxGeometry,
	BufferGeometry,
	Float32BufferAttribute,
	Matrix4,
	Camera,
	Vector3,
	Quaternion,
	DoubleSide,
	Material,
	LineSegments,
	LineBasicMaterial,
	Box3,
	Sprite,
	TextureLoader,
	SpriteMaterial,
} from "three";
import { Three } from "../lib/Three";
import { createNDCPlane, NDCPlaneRefs } from "../lib/NDCPlane";
import { twSize } from "../lib/standardStyles";
import { textStyles } from "../lib/textStyles";
import normalMouseCursor from "../assets/mouseCursors/normal.svg";
import diagonalResizeMouseCursor from "../assets/mouseCursors/resize.svg";

// ======================================================
// FUNÇÃO PRINCIPAL DA CENA
// ======================================================

export default makeScene2D(function* perspectiveToOrtho(view: View2D) {
	const viewportRef = createRef<Rect>();
	view.add(<Rect width={"100%"} height={"100%"} ref={viewportRef} stroke={"#ffffff7f"} lineWidth={3} />);

	const scene = new Scene();
	const { tri, triMat, square, squareMat, filledSquare, filledMat } = createGeometries(scene);
	const { cube, materials: cubeMaterials } = createCube(scene);
	const { persp, ortho } = createCameras(viewportRef);

	// ==== Signals ====
	const projectionBlendValue = createSignal(1) as SimpleSignal<number>; // 1=ortho, 0=persp
	const cubeRotationY = createSignal(0) as SimpleSignal<number>;
	const cubeRotationX = createSignal(0) as SimpleSignal<number>;
	const cubeZ = createSignal(0) as SimpleSignal<number>;

	// câmara blendada usada no viewport da esquerda
	const camSig = createSignal<Camera>(blendCamera(persp, ortho, projectionBlendValue())) as SimpleSignal<Camera>;

	// Reatividade
	createEffect(() => {
		// atualiza câmara blendada e garante que não renderiza helpers (layer 1)
		const cam = blendCamera(persp, ortho, projectionBlendValue());
		cam.layers.disable(1);
		camSig(cam);
	});
	createEffect(() => (cube.rotation.y = cubeRotationY()));
	createEffect(() => (cube.rotation.x = cubeRotationX()));
	createEffect(() => {
		cube.position.z = cubeZ();
	});

	// Render principal (esquerda)
	viewportRef().add(
		<Three
			scene={scene}
			camera={camSig()}
			width={() => viewportRef().width()}
			height={() => viewportRef().height()}
			onRender={r => r.render(scene, camSig())}
		/>,
	);

	// ==== Abertura: triângulo → quadrado → cubo ====
	yield* SA_shapes(tri, triMat, square, squareMat, filledSquare, filledMat, cubeMaterials, projectionBlendValue);
	yield* cubeRotationY(Math.PI, 1, easeInOutCubic);

	yield* beginSlide("Cubo em perspetiva");
	yield* all(projectionBlendValue(1, 1.2, easeInOutCubic));
	yield* beginSlide("Modo ortogonal OpenGL: matriz identidade");

	const plane = createNDCPlane(viewportRef());
	const ndcTitle = createRef<Rect>();
	const ndcTitleText = createRef<Txt>();
	view.add(
		<Rect layout padding={twSize(4)} ref={ndcTitle} fill="#f58d42" smoothCorners radius={20}>
			<Txt {...textStyles.heading2} fontFamily={"Fira Code"} fill={"#000"} ref={ndcTitleText}>
				Sistema de Coordenadas Normalizado
			</Txt>
		</Rect>,
	);

	yield* all(plane.container().opacity(0).opacity(1.0, 0.5), ndcTitle().y(-680).y(-438, 0.5));
	yield* beginSlide("Sistema de Coordenadas Normalizado");
	yield* SA_ndcPlane(view, viewportRef, plane, ndcTitle, ndcTitleText);

	// ==== Comparação lado-a-lado (ortho à esquerda, vista lateral à direita) + transição para perspetiva ====
	const side = initSideViewState();
	yield* SA_orthoProjectionDemo(view, viewportRef, scene, camSig, ortho, cubeZ, side);
	yield* SA_perspectiveProjectionDemo(view, scene, ortho, persp, camSig, projectionBlendValue, cubeZ, side, ndcTitle, ndcTitleText, viewportRef);
});

// ======================================================
// FUNÇÕES DE CRIAÇÃO DE CENA
// ======================================================

function blendCamera(persp: PerspectiveCamera, ortho: OrthographicCamera, k: number): Camera {
	const proj = new Matrix4();
	for (let i = 0; i < 16; i++) {
		proj.elements[i] = persp.projectionMatrix.elements[i] * (1 - k) + ortho.projectionMatrix.elements[i] * k;
	}

	const pos = new Vector3().lerpVectors(persp.position, ortho.position, k);
	const quat = new Quaternion().slerpQuaternions(persp.quaternion, ortho.quaternion, k);

	const cam = new Camera();
	cam.projectionMatrix.copy(proj);
	cam.position.copy(pos);
	cam.quaternion.copy(quat);
	cam.updateMatrixWorld(true);
	return cam;
}

function createGeometries(scene: Scene) {
	const triGeo = new BufferGeometry();
	triGeo.setAttribute("position", new Float32BufferAttribute([0, 0.5, 0, -0.5, -0.5, 0, 0.5, -0.5, 0], 3));
	const triMat = new MeshBasicMaterial({ color: 0x00ffff, transparent: true, opacity: 1 });
	const tri = new Mesh(triGeo, triMat);
	scene.add(tri);

	const squareGeo = new BufferGeometry();
	squareGeo.setAttribute(
		"position",
		new Float32BufferAttribute([-0.5, 0.5, 0, 0.5, 0.5, 0, -0.5, -0.5, 0, 0.5, 0.5, 0, 0.5, -0.5, 0, -0.5, -0.5, 0], 3),
	);

	const squareMat = new MeshBasicMaterial({
		color: 0x0099ff,
		wireframe: true,
		transparent: true,
		opacity: 0,
		depthWrite: false,
	});
	const square = new Mesh(squareGeo, squareMat);
	scene.add(square);

	const filledMat = new MeshBasicMaterial({
		color: 0x0099ff,
		wireframe: false,
		transparent: true,
		opacity: 0,
		side: DoubleSide,
		depthWrite: false,
	});
	const filledSquare = new Mesh(squareGeo.clone(), filledMat);
	filledSquare.position.z = 0.5;
	scene.add(filledSquare);

	return { tri, triMat, square, squareMat, filledSquare, filledMat };
}

function createCube(scene: Scene) {
	const faceColors = [0x2ec27e, 0x3584e4, 0xe01b24, 0xf5c211, 0x9141ac, 0x33d17a];
	const materials = faceColors.map(c => new MeshBasicMaterial({ color: c, transparent: true, opacity: 0.0, side: DoubleSide }));
	const cube = new Mesh(new BoxGeometry(1, 1, 1), materials);
	scene.add(cube);
	return { cube, materials };
}

function createCameras(viewportRef: Reference<Rect>) {
	const aspect = () => viewportRef().width() / viewportRef().height();

	const persp = new PerspectiveCamera(60, aspect(), 0.1, 10);
	createEffect(() => {
		persp.aspect = aspect();
		persp.updateProjectionMatrix();
		persp.updateMatrixWorld(true);
	});
	persp.position.set(2.5, 2.5, 2.5);
	persp.lookAt(0, 0, 0);
	persp.updateMatrixWorld(true);

	// Viewport OpenGL (sem aspect ratio, -1..1 NDC)
	const ortho = new OrthographicCamera(-1, 1, 1, -1, -10, 10);
	ortho.position.set(0, 0, 0);
	ortho.rotation.set(0, 0, 0);
	ortho.updateMatrixWorld(true);

	return { persp, ortho };
}

// ======================================================
// SUBANIMAÇÕES (SA_*)
// ======================================================

function* SA_shapes(
	tri: Mesh,
	triMat: Material,
	square: Mesh,
	squareMat: Material,
	filledSquare: Mesh,
	filledMat: Material,
	cubeMaterials: Material[],
	projectionBlendValue: SimpleSignal<number>,
) {
	yield* beginSlide("Triângulo");

	yield* tween(1, v => {
		const e = easeInOutCubic(v);
		(triMat as MeshBasicMaterial).opacity = 1 - e;
		(squareMat as MeshBasicMaterial).opacity = e;
	});
	tri.removeFromParent();

	yield* beginSlide("Quadrado wireframe");
	yield* tween(0.8, v => {
		const e = easeInOutCubic(v);
		(squareMat as MeshBasicMaterial).opacity = 1 - e;
		(filledMat as MeshBasicMaterial).opacity = e;
	});
	square.removeFromParent();

	yield* beginSlide("Quadrado sólido");
	yield* all(
		projectionBlendValue(0, 1.2, easeInOutCubic),
		tween(1.2, v => {
			const e = easeInOutCubic(v);
			(filledMat as MeshBasicMaterial).opacity = 1 - e;
			cubeMaterials.forEach(m => {
				(m as MeshBasicMaterial).opacity = e;
			});
		}),
	);
	filledSquare.removeFromParent();
}

function* SA_ndcPlane(view: View2D, viewportRef: Reference<Rect>, plane: NDCPlaneRefs, ndcTitle: Reference<Rect>, ndcTitleText: Reference<Txt>) {
	const cursor = createRef<Img>();
	view.add(<Img ref={cursor} src={diagonalResizeMouseCursor} width={twSize("7")} height={twSize("7")} opacity={0} zIndex={100} />);

	cursor().position(() => {
		const vp = viewportRef();
		return new Vector2(vp.x() + vp.width() / 2, vp.y() + vp.height() / 2);
	});

	yield* cursor().opacity(1, 0.5);

	yield* all(
		viewportRef().width("40%", 0.5, easeHumanMotionSmooth),
		viewportRef().height("70%", 0.5, easeHumanMotionSmooth),
		viewportRef().y(150, 0.5, easeHumanMotionSmooth),
	);

	yield* waitFor(0.2);
	yield* all(viewportRef().width("70%", 0.5, easeHumanMotionSmooth), viewportRef().height("50%", 0.5, easeHumanMotionSmooth));

	// animação circular
	const baseW = viewportRef().width();
	const baseH = viewportRef().height();
	const A = 0.3;
	let phi0 = 0;
	let A0 = A;
	const rotations = 2.7;
	const duration = 1.7;

	yield* tween(duration, t => {
		const e = easeLinearFalloff(0.8)(t);
		const angle = phi0 + e * Math.PI * 2 * rotations;
		const Aeff = A0 + (A - A0) * Math.min(t / 0.25, 1);
		const newW = baseW * (1 + Aeff * Math.cos(angle));
		const newH = baseH * (1 + Aeff * Math.sin(angle));
		viewportRef().width(newW);
		viewportRef().height(newH);
	});

	// movimento do cursor
	cursor().src(normalMouseCursor);
	const start = cursor().position();
	const totalX = 120;
	const totalY = 200;
	yield* waitFor(1.0);
	yield* tween(0.3, t => {
		t = easeHumanMotionSmooth(t);
		const dx = totalX * t;
		const dy = -totalY * (1 - Math.cos((t * Math.PI) / 2));
		cursor().position(new Vector2(start.x + dx, start.y + dy));
	});
	yield* waitFor(1.0);
	yield* all(cursor().position.add([1000, 100], 0.4, easeHumanMotionSmooth), plane.container().opacity(0.0, 0.4));

	yield* beginSlide("Redimensionar viewport");
	yield* all(ndcTitle().fill("#3483eb", 1), ndcTitleText().text("Projeção Ortogonal (2D)", 0.4));
	yield* beginSlide("Projeção Ortogonal");
}

// ======================================================
// (continua na Parte 2/2: helpers de frustum com LineSegments,
// layers, state do viewport direito e subanimações finais,
// + funções de easing)
// ======================================================
// ======================================================
// STATE / HELPERS: VIEWPORT DA DIREITA (REUTILIZÁVEL) + FRUSTUMS
// ======================================================

type SideViewState = {
	viewportSideRef: Reference<Rect>;
	sideCam: PerspectiveCamera;
	orthoFrustum?: LineSegments;
	perspFrustum?: LineSegments;
	cameraSprite?: Sprite;
};

function initSideViewState(): SideViewState {
	return {
		viewportSideRef: createRef<Rect>(),
		sideCam: new PerspectiveCamera(60, 1, 0.1, 100),
	};
}

/**
 * Garante que existe o viewport da direita e o <Three/> correspondente (apenas uma vez).
 * Também aplica a política de layers:
 *  - cam do ecrã (camSig) NÃO vê helpers (layer 1)
 *  - sideCam vê helpers (layer 1)
 */
function ensureSideView(
	view: View2D,
	viewLeft: Reference<Rect>, // não é necessário aqui, mantido por compatibilidade
	scene: Scene,
	camSig: SimpleSignal<Camera>,
	state: SideViewState,
) {
	// configura sideCam
	state.sideCam.position.set(3, 2, -5);
	state.sideCam.lookAt(0, 0, 0);
	state.sideCam.layers.enable(1);
	state.sideCam.updateMatrixWorld(true);

	// cria o rect do lado direito
	view.add(<Rect ref={state.viewportSideRef} width={"45%"} height={"55%"} stroke={"#ffffff7f"} lineWidth={3} x={view.width() / 4} y={0} />);

	// ajusta o aspect da sideCam reativamente ao tamanho do rect
	createEffect(() => {
		const w = Number(state.viewportSideRef().width());
		const h = Number(state.viewportSideRef().height());
		const ar = h === 0 ? 1 : w / h;
		if (Math.abs(state.sideCam.aspect - ar) > 1e-6) {
			state.sideCam.aspect = ar;
			state.sideCam.updateProjectionMatrix();
		}
	});

	// adiciona o Three com a MESMA scene mas câmara lateral
	state
		.viewportSideRef()
		.add(
			<Three
				scene={scene}
				camera={state.sideCam}
				width={() => state.viewportSideRef().width()}
				height={() => state.viewportSideRef().height()}
				onRender={r => r.render(scene, state.sideCam)}
			/>,
		);

	// câmara do ecrã NÃO vê helpers (layer 1)
	camSig().layers.disable(1);
}

// ---------- Frustums sem diagonais internas (LineSegments) ----------

function createOrthoFrustumHelper(ortho: OrthographicCamera): LineSegments {
	const verts = [
		// near
		[ortho.left, ortho.top, -1],
		[ortho.right, ortho.top, -1],
		[ortho.right, ortho.top, -1],
		[ortho.right, ortho.bottom, -1],
		[ortho.right, ortho.bottom, -1],
		[ortho.left, ortho.bottom, -1],
		[ortho.left, ortho.bottom, -1],
		[ortho.left, ortho.top, -1],
		// far
		[ortho.left, ortho.top, 1],
		[ortho.right, ortho.top, 1],
		[ortho.right, ortho.top, 1],
		[ortho.right, ortho.bottom, 1],
		[ortho.right, ortho.bottom, 1],
		[ortho.left, ortho.bottom, 1],
		[ortho.left, ortho.bottom, 1],
		[ortho.left, ortho.top, 1],
		// edges
		[ortho.left, ortho.top, -1],
		[ortho.left, ortho.top, 1],
		[ortho.right, ortho.top, -1],
		[ortho.right, ortho.top, 1],
		[ortho.right, ortho.bottom, -1],
		[ortho.right, ortho.bottom, 1],
		[ortho.left, ortho.bottom, -1],
		[ortho.left, ortho.bottom, 1],
	].flat();

	const g = new BufferGeometry();
	g.setAttribute("position", new Float32BufferAttribute(verts, 3));
	const m = new LineBasicMaterial({ color: 0xffff00 });
	const lines = new LineSegments(g, m);
	lines.position.copy(ortho.position);
	lines.rotation.copy(ortho.rotation);
	lines.layers.set(1); // helpers na layer 1
	lines.updateMatrixWorld(true);
	return lines;
}

function createPerspectiveFrustumHelper(persp: PerspectiveCamera): LineSegments {
	const fov = persp.fov * (Math.PI / 180);
	const aspect = persp.aspect;
	const near = persp.near,
		far = 1.4;
	const hN = 2 * Math.tan(fov / 2) * near,
		wN = hN * aspect;
	const hF = 2 * Math.tan(fov / 2) * far,
		wF = hF * aspect;

	const verts = [
		// near
		[-wN / 2, hN / 2, -near],
		[wN / 2, hN / 2, -near],
		[wN / 2, hN / 2, -near],
		[wN / 2, -hN / 2, -near],
		[wN / 2, -hN / 2, -near],
		[-wN / 2, -hN / 2, -near],
		[-wN / 2, -hN / 2, -near],
		[-wN / 2, hN / 2, -near],
		// far
		[-wF / 2, hF / 2, -far],
		[wF / 2, hF / 2, -far],
		[wF / 2, hF / 2, -far],
		[wF / 2, -hF / 2, -far],
		[wF / 2, -hF / 2, -far],
		[-wF / 2, -hF / 2, -far],
		[-wF / 2, -hF / 2, -far],
		[-wF / 2, hF / 2, -far],
		// edges
		[-wN / 2, hN / 2, -near],
		[-wF / 2, hF / 2, -far],
		[wN / 2, hN / 2, -near],
		[wF / 2, hF / 2, -far],
		[wN / 2, -hN / 2, -near],
		[wF / 2, -hF / 2, -far],
		[-wN / 2, -hN / 2, -near],
		[-wF / 2, -hF / 2, -far],
	].flat();

	const g = new BufferGeometry();
	g.setAttribute("position", new Float32BufferAttribute(verts, 3));
	const m = new LineBasicMaterial({ color: 0xff8000 });
	const lines = new LineSegments(g, m);
	lines.position.copy(persp.position);
	lines.rotation.copy(persp.rotation);
	lines.layers.set(1); // helpers na layer 1
	lines.updateMatrixWorld(true);
	return lines;
}

// ======================================================
// SUBANIMAÇÕES FINAIS (lado-a-lado + transição perspetiva)
// ======================================================

function* SA_orthoProjectionDemo(
	view: View2D,
	viewportLeft: Reference<Rect>,
	scene: Scene,
	camSig: SimpleSignal<Camera>,
	ortho: OrthographicCamera,
	cubeZ: SimpleSignal<number>,
	side: SideViewState,
) {
	yield* beginSlide("Comparação ortogonal");

	// posiciona e dimensiona o viewport da esquerda
	yield* all(
		viewportLeft().width("45%", 1, easeInOutCubic),
		viewportLeft().height("55%", 1, easeInOutCubic),
		viewportLeft().x(-view.width() / 4, 1, easeInOutCubic),
		viewportLeft().y(0, 1, easeInOutCubic),
	);

	// garante viewport direito e política de layers
	ensureSideView(view, viewportLeft, scene, camSig, side);

	// helpers ortográficos visíveis; persp escondido
	if (!side.orthoFrustum) {
		side.orthoFrustum = createOrthoFrustumHelper(ortho);
		scene.add(side.orthoFrustum);
	}
	side.orthoFrustum.visible = true;

	if (side.perspFrustum) side.perspFrustum.visible = false;

	const camIcon = ensureCameraBillboard(side, scene, camSig);
	camIcon.visible = true;
	// legenda
	const caption = createRef<Txt>();
	view.add(
		<Txt
			ref={caption}
			{...textStyles.normal}
			y={view.height() / 2 - 80}
			text="Na projeção ortogonal, a profundidade não deixa os objetos mais pequenos."
			opacity={0}
		/>,
	);
	yield* caption().opacity(1, 1);

	// movimento do cubo via signal (afeta ambas as vistas)
	const start = cubeZ();
	yield* tween(2.5, e => {
		cubeZ(start + Math.sin(e * Math.PI * 2) * 1.5);
	});

	yield* caption().opacity(0, 0.6);
	caption().remove();
}

function* SA_perspectiveProjectionDemo(
	view: View2D,
	scene: Scene,
	ortho: OrthographicCamera,
	persp: PerspectiveCamera,
	camSig: SimpleSignal<Camera>,
	projectionBlendValue: SimpleSignal<number>,
	cubeZ: SimpleSignal<number>,
	side: SideViewState,
	ndcTitle: Reference<Rect>,
	ndcTitleText: Reference<Txt>,
	viewportRef: Reference<Rect>,
) {
	yield* beginSlide("Projeção de Perspetiva");

	yield* all(ndcTitle().fill("#3483eb", 1), ndcTitleText().text("Projeção de Perspectiva (3D)", 0.4));
	// garante viewport direito e layers
	ensureSideView(view, createRef<Rect>() as any, scene, camSig, side);

	// mostrar persp frustum; esconder ortho
	if (!side.perspFrustum) {
		side.perspFrustum = createPerspectiveFrustumHelper(persp);
		scene.add(side.perspFrustum);
	}
	side.perspFrustum.visible = false;

	if (side.orthoFrustum) side.orthoFrustum.visible = false;

	const camSprite = ensureCameraBillboard(side, scene, camSig);
	camSprite.visible = true;

	// legenda
	const caption = createRef<Txt>();
	view.add(
		<Txt
			ref={caption}
			{...textStyles.normal}
			y={view.height() / 2 - 110}
			text={"A projeção perspectiva adiciona noção de profundidade\nsimulando como vemos objetos na vida real."}
			opacity={0}
			textAlign={"center"}
		/>,
	);
	yield* all(projectionBlendValue(0.0, 1.0), caption().opacity(1, 1));
	side.perspFrustum.visible = true;
	// demonstra a perspetiva (escala varia com Z)
	yield* beginSlide("Profundidade na perspetiva");
	const base = cubeZ();
	yield* tween(2.5, e => {
		cubeZ(base + Math.sin(e * Math.PI * 2) * 2);
	});
	yield* beginSlide("Resolve esticamento");
	yield* caption().text("Alem disso, resolve o problema de esticar os objetos\nporque ele usa a proporção do ecrã na fórmula", 1.0);
	yield* beginSlide("Matriz de projeção");
	const projectionMatrixRef = createRef<Img>();
	view.add(<Img src={projectionMatrix} ref={projectionMatrixRef} opacity={0} />);
	yield* projectionMatrixRef().opacity(1, 0.4);
}
function ensureCameraBillboard(state: SideViewState, scene: Scene, camera: SimpleSignal<Camera>): Sprite {
	const spriteUrl = "/photo_camera_34dp_E3E3E3_FILL0_wght400_GRAD0_opsz40.png";
	let sprite: Sprite;
	createEffect(() => {
		(sprite ?? state.cameraSprite)?.position.copy(camera().position);
	});
	if (state.cameraSprite) return state.cameraSprite;

	const tex = new TextureLoader().load(spriteUrl);
	// opções para nitidez/transparência; ajusta se quiseres
	// tex.magFilter = NearestFilter; // se quiseres pixel-art
	// tex.minFilter = LinearFilter;
	const mat = new SpriteMaterial({
		map: tex,
		transparent: true,
		depthWrite: false, // para não “sujar” o depth buffer
		depthTest: true, // continua a respeitar o z das linhas do frustum
	});

	sprite = new Sprite(mat);
	sprite.layers.set(1); // só a sideCam (que tem layer 1 enabled) vai ver
	// sprite.scale.set(0.4, 0.3, 1); // tamanho em unidades de mundo (ajusta ao gosto)

	scene.add(sprite);
	state.cameraSprite = sprite;
	return sprite;
}

// ======================================================
// FUNÇÕES DE EASING
// ======================================================

export function easeHumanMotionSmooth(t: number, from: number = 0, to: number = 1): number {
	t = Math.min(Math.max(t, 0), 1);
	const accelPower = 0.4;
	const decelPower = 1.8;
	const phase = Math.pow(t, accelPower) * (1 - Math.pow(t, decelPower)) + Math.pow(t, decelPower);
	const s = 1 / (1 + Math.exp(-10 * (t - 0.5)));
	const smooth = phase * s + t * (1 - s);
	return from + (to - from) * smooth;
}

export const easeLinearFalloff =
	(falloffStart: number = 0.8) =>
	(t: number, from: number = 0, to: number = 1): number => {
		t = Math.min(Math.max(t, 0), 1);
		falloffStart = Math.min(Math.max(falloffStart, 0), 1);

		let result: number;
		if (t <= falloffStart) {
			result = t;
		} else {
			const s = (t - falloffStart) / (1 - falloffStart);
			const decel = 1 - (1 - s) * (1 - s);
			result = falloffStart + (1 - falloffStart) * decel * 0.5;
		}
		return from + (to - from) * result;
	};
