import { brightness, Img, Line, makeScene2D, Node, Rect, Txt, View2D } from "@motion-canvas/2d";
import {
	all,
	beginSlide,
	createRef,
	createSignal,
	delay,
	easeInOutQuint,
	waitFor,
	waitTransition,
	Vector2,
	SimpleSignal,
	Reference,
} from "@motion-canvas/core";
import { textStyles } from "../lib/textStyles";
import { badgeStyle, cardStyle, twFontSize, twSize } from "../lib/standardStyles";
import gpuImage from "../assets/placa gráfica.png";
import driverIcon from "../assets/driver.svg";
import vulkanLogo from "../assets/vulkan.png";
import directXLogo from "../assets/directx.png";
import gameSvg from "../assets/game.svg";

const openglLogo =
	"https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Opengl-logo.svg/2560px-Opengl-logo.svg.png";

// ======================================================
// === CENA PRINCIPAL
// ======================================================
export default makeScene2D(function* (view: View2D) {
	const commandAnimationPlaybackSpeed = createSignal(1) as SimpleSignal<number>;
	const visualizationRootNodeOpacity = createSignal(0) as SimpleSignal<number>;

	// --- Setup inicial
	const transitionDuration = 0.6;
	const openglTitleRef = createRef<Txt>();
	view.add(<Txt ref={openglTitleRef} position={[-514, 105]} {...textStyles.subtitle} textAlign="center" text={"OpenGL"} />);
	openglTitleRef().save();

	yield* all(
		waitTransition(0.6, true),
		openglTitleRef().position([0, 0], transitionDuration),
		openglTitleRef().fontWeight(textStyles.heading1.fontWeight, 1.0),
		openglTitleRef().fill(textStyles.heading1.fill, 1.0),
		openglTitleRef().fontFamily(textStyles.heading1.fontFamily, 1.0),
		openglTitleRef().fontSize(textStyles.heading1.fontSize, 1.0),
	);

	// --- Início da narrativa
	yield* SA_intro(openglTitleRef);
	yield* SA_vulkanDirectXComparison(view);
	yield* SA_defineOpenGL(openglTitleRef);

	const { gameIconRef, driverIconRef, gpuIconRef } = createOpenGLFlowVisualization(view, visualizationRootNodeOpacity);
	yield* SA_flowSetup(openglTitleRef, visualizationRootNodeOpacity);
	yield* SA_sendCommands(view, { gameIconRef, driverIconRef, gpuIconRef }, commandAnimationPlaybackSpeed, visualizationRootNodeOpacity);
});

// ======================================================
// === SUBANIMAÇÕES (SA_*)
// ======================================================

function* SA_intro(openglTitleRef: Reference<Txt>) {
	yield* beginSlide("O que é opengl?");
	yield* openglTitleRef().text("OpenGL é uma API gráfica", 0.5);
	yield* beginSlide("OpenGL é uma API gráfica");
}

function* SA_vulkanDirectXComparison(view: View2D) {
	const vulkanLogoRef = createRef<Img>();
	const directXLogoRef = createRef<Img>();

	view.add(<Img src={vulkanLogo} y={view.size().y / 1.5} ref={vulkanLogoRef} />);
	yield* vulkanLogoRef().y(300, 0.8, easeInOutQuint);
	yield* beginSlide("Vulkan");

	view.add(<Img src={directXLogo} y={view.size().y / 1.5} ref={directXLogoRef} />);
	yield vulkanLogoRef().y(view.size().y / 1.5, 0.8, easeInOutQuint);
	yield* directXLogoRef().y(300, 0.8, easeInOutQuint);
	yield* beginSlide("DirectX");
	yield directXLogoRef().y(view.size().y / 1.5, 0.8, easeInOutQuint);

	vulkanLogoRef().remove();
	directXLogoRef().remove();
}

function* SA_defineOpenGL(openglTitleRef: Reference<Txt>) {
	yield* openglTitleRef().text("OpenGL é ", 0.5);
	yield* openglTitleRef().fontSize(twFontSize("6xl"), 0.5);
	yield* openglTitleRef().text("OpenGL é um padrão para falar\ncom placas gráficas", 0.5);
	yield* beginSlide("Em outras palavras");
	yield* openglTitleRef().text("OpenGL ", 0.2);
}

function* SA_flowSetup(openglTitleRef: Reference<Txt>, visualizationRootNodeOpacity: SimpleSignal<number>) {
	yield* all(
		openglTitleRef().restore(0.6),
		openglTitleRef().position([-800, 530], 0.6),
		openglTitleRef().text("1. OpenGL", 0.6),
		delay(0.4, visualizationRootNodeOpacity(1, 0.25)),
	);
	yield* beginSlide("Animação do funcionamento do opengl");
}

function* SA_sendCommands(
	view: View2D,
	anchorPoints: Record<"gameIconRef" | "driverIconRef" | "gpuIconRef", Reference<Rect>>,
	commandAnimationPlaybackSpeed: SimpleSignal<number>,
	visualizationRootNodeOpacity: SimpleSignal<number>,
) {
	yield* SA_sendOpenGLCommand(view, NormalOpenGLCommand({ command: "glUniform1f(2, 60);" }), anchorPoints, commandAnimationPlaybackSpeed, visualizationRootNodeOpacity);
	yield* SA_sendOpenGLCommand(view, NormalOpenGLCommand({ command: "glDrawArrays(GL_LINES, 0, 6);" }), anchorPoints, commandAnimationPlaybackSpeed, visualizationRootNodeOpacity);
	yield* waitFor(0.5);
	yield* beginSlide("Enviar comandos");
}

// ======================================================
// === SUBANIMAÇÃO: ENVIO DE COMANDOS OPENGL
// ======================================================

function* SA_sendOpenGLCommand(
	view: View2D,
	command: Node,
	anchorPoints: Record<"gameIconRef" | "driverIconRef" | "gpuIconRef", Reference<Rect>>,
	commandAnimationPlaybackSpeed: SimpleSignal<number>,
	visualizationRootNodeOpacity: SimpleSignal<number>,
) {
	const openGLCommandCardRef = createRef<Rect>();
	const binaryCommandCardRef = createRef<Rect>();

	// --- Comando textual (lado esquerdo)
	view.add(
		<Rect
			left={createSignal(() => anchorPoints.gameIconRef().right())}
			top={createSignal(() => anchorPoints.driverIconRef().top())}
			width={Math.abs(anchorPoints.driverIconRef().left().x - anchorPoints.gameIconRef().right().x)}
			height={anchorPoints.driverIconRef().height()}
			opacity={visualizationRootNodeOpacity}
			clip
		>
			<Rect {...badgeStyle} ref={openGLCommandCardRef} left={anchorPoints.gameIconRef().right()} layout>
				{command}
			</Rect>
		</Rect>,
	);

	// --- Comando binário (lado direito)
	view.add(
		<Rect
			opacity={visualizationRootNodeOpacity}
			left={createSignal(() => anchorPoints.driverIconRef().right())}
			top={createSignal(() => anchorPoints.gpuIconRef().top())}
			width={Math.abs(anchorPoints.gpuIconRef().left().x - anchorPoints.driverIconRef().right().x)}
			height={anchorPoints.gpuIconRef().height()}
			clip
		>
			<Rect {...badgeStyle} fill={"#00ff48ff"} ref={binaryCommandCardRef} offsetX={2.5} layout>
				<Txt {...textStyles.code} fill="#000">
					00101010110101101010101010
				</Txt>
			</Rect>
		</Rect>,
	);

	// --- Animação de envio
	yield* openGLCommandCardRef().position.x(498, 1.7 / commandAnimationPlaybackSpeed());
	yield SA_activateBorder(anchorPoints.driverIconRef);
	yield* binaryCommandCardRef().position.x(view.width() / 2, 1.5 / commandAnimationPlaybackSpeed());
	yield SA_activateBorder(anchorPoints.gpuIconRef);
}

function* SA_activateBorder(ref: Reference<Rect>) {
	ref().stroke("#FFFFFF");
	yield* ref().lineWidth(4, 0.0);
	yield* ref().lineWidth(0, 0.6);
}

// ======================================================
// === FUNÇÕES AUXILIARES DE VISUALIZAÇÃO
// ======================================================

function createOpenGLFlowVisualization(view: View2D, opacity: SimpleSignal<number>) {
	const visualizationRootNodeRef = createRef<Rect>();
	const gameIconRef = createRef<Rect>();
	const driverIconRef = createRef<Rect>();
	const gpuIconRef = createRef<Rect>();

	view.add(
		<Rect layout ref={visualizationRootNodeRef} opacity={opacity} alignItems={"center"}>
			<Rect layout ref={gameIconRef} direction={"column"} alignItems={"center"} gap={twSize("2.5")} {...cardStyle}>
				<Img height={100} src={gameSvg} />
				<Txt text="Jogo" {...textStyles.caption} />
			</Rect>
			{connector(400 - 30 - 100)}
			<Img src={openglLogo} width={100} />
			{connector(30)}
			<Rect layout ref={driverIconRef} direction={"column"} alignItems={"center"} gap={twSize("4")} {...cardStyle}>
				<Img height={100} src={driverIcon} />
				<Txt text="Driver de Vídeo" {...textStyles.caption} />
			</Rect>
			{connector()}
			<Rect layout ref={gpuIconRef} direction={"column"} alignItems={"center"} gap={twSize("4")} {...cardStyle}>
				<Img height={100} filters={[brightness(1.3)]} src={gpuImage} />
				<Txt text="Placa Gráfica" {...textStyles.caption} />
			</Rect>
		</Rect>,
	);

	return { gameIconRef, driverIconRef, gpuIconRef, visualizationRootNodeRef };
}

function connector(connectorWidth = 400, ref?: Reference<Line>) {
	return (
		<Line
			stroke={"#ffffff4f"}
			lineWidth={twSize("1")}
			ref={ref}
			points={[
				[0, 0],
				[connectorWidth, 0],
			]}
		/>
	);
}

// ======================================================
// === ELEMENTOS DE TEXTO
// ======================================================

function NormalOpenGLCommand(props: { command: string }) {
	return <Txt.b text={props.command} zIndex={5} {...textStyles.code} fill={"#000"} />;
}
