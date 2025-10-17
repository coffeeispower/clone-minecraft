import { Rect, Grid, Line, Txt } from "@motion-canvas/2d";
import { createRef, Reference } from "@motion-canvas/core";
import { twFontSize } from "./standardStyles";

export interface NDCPlaneRefs {
	container: Reference<Rect>;
	gridMinor: Reference<Grid>;
	gridMajor: Reference<Grid>;
	xAxis: Reference<Line>;
	yAxis: Reference<Line>;
	ticksX: Reference<Line>[];
	ticksY: Reference<Line>[];
	labels: Reference<Txt>[];
}

/**
 * Cria um plano cartesiano normalizado ([-1,1]) estilo OpenGL NDC.
 * Transparente e com texto legível em qualquer fundo.
 */
export function createNDCPlane(parent: Rect): NDCPlaneRefs {
	const gridMinor = createRef<Grid>();
	const gridMajor = createRef<Grid>();
	const xAxis = createRef<Line>();
	const yAxis = createRef<Line>();
	const container = createRef<Rect>();
	const ticksX: Reference<Line>[] = [];
	const ticksY: Reference<Line>[] = [];
	const labels: Reference<Txt>[] = [];

	const W = () => parent.width();
	const H = () => parent.height();

	const GRID_MAJOR = "ffffff40";
	const GRID_MINOR = "#ffffff20";
	const AXIS = "#ffffff";

	const stepNDC = 0.1;
	const stepX = () => (W() / 2) * stepNDC;
	const stepY = () => (H() / 2) * stepNDC;
	const majorX = () => (W() / 2) * 0.5;
	const majorY = () => (H() / 2) * 0.5;

	parent.add(
		<Rect ref={container} width={W} height={H} clip>
			{/* grelha */}
			<Grid ref={gridMinor} width={W} height={H} stroke={GRID_MINOR} lineWidth={1} spacing={() => [stepX(), stepY()]} />
			<Grid ref={gridMajor} width={W} height={H} stroke={GRID_MAJOR} lineWidth={2} spacing={() => [majorX(), majorY()]} />

			{/* eixos */}
			<Line
				ref={xAxis}
				points={() => [
					[-W() / 2, 0],
					[W() / 2, 0],
				]}
				stroke={AXIS}
				lineWidth={4}
				endArrow
			/>
			<Line
				ref={yAxis}
				points={() => [
					[0, H() / 2],
					[0, -H() / 2], // seta agora aponta para cima ✅
				]}
				stroke={AXIS}
				lineWidth={4}
				endArrow
			/>

			{/* ticks e labels no eixo X */}
			{[-1, -0.5, 0.5, 1].flatMap(x => {
				if (x === 0) return [];
				const px = () => (W() / 2) * x;
				const tickRef = createRef<Line>();
				const labelRef = createRef<Txt>();
				ticksX.push(tickRef);
				labels.push(labelRef);
				return [
					<Line
						ref={tickRef}
						points={() => [
							[px(), -8],
							[px(), 8],
						]}
						stroke={AXIS}
						lineWidth={3}
					/>,
					<Txt
						ref={labelRef}
						x={() => Math.min(Math.abs(px()) + 40, W() / 2 - 40) * Math.sign(px())}
						y={-34}
						fill={AXIS}
						fontSize={twFontSize("2xl")}
						textAlign="center"
					>
						{x.toString()}
					</Txt>,
				];
			})}

			{/* ticks e labels no eixo Y */}
			{[-1, -0.5, 0.5, 1].flatMap(y => {
				if (y === 0) return [];
				const py = () => (H() / 2) * y;
				const tickRef = createRef<Line>();
				const labelRef = createRef<Txt>();
				ticksY.push(tickRef);
				labels.push(labelRef);
				return [
					<Line
						ref={tickRef}
						points={() => [
							[-8, py()],
							[8, py()],
						]}
						stroke={AXIS}
						lineWidth={3}
					/>,
					<Txt
						ref={labelRef}
						x={50}
						y={() => Math.min(Math.abs(py()) + 40, H() / 2 - 30) * Math.sign(py())}
						fill={AXIS}
						fontSize={twFontSize("2xl")}
						textAlign="left"
					>
						{y.toString()}
					</Txt>,
				];
			})}

			{/* labels dos eixos */}
			<Txt x={() => W() / 2 - 60} y={24} fill={AXIS} fontSize={twFontSize("4xl")}>
				x
			</Txt>
			<Txt x={-24} y={() => -(H() / 2) + 60} fill={AXIS} fontSize={twFontSize("4xl")}>
				y
			</Txt>
		</Rect>,
	);

	return {
		container,
		gridMinor,
		gridMajor,
		xAxis,
		yAxis,
		ticksX,
		ticksY,
		labels,
	};
}
