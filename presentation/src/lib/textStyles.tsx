import type { TxtProps } from "@motion-canvas/2d";
import { twFontSize } from "./standardStyles";
export const textStyles = {
	normal: {
		fill: "#C5D6F0", // azul claro
		fontSize: twFontSize("xl"),
		fontWeight: 700,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.5,
		lineHeight: "140%",
	},
	caption: {
		fill: "#C5D6F0", // azul claro
		fontSize: twFontSize("xl"),
		fontWeight: 700,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.5,
		lineHeight: "140%",
	},
	dimmed: {
		fill: "#77798f", // azul claro
		fontSize: twFontSize("base"),
		fontWeight: 700,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.5,
		lineHeight: "140%",
	},
	heading1: {
		fill: "#55C667", // verde grama
		fontSize: twFontSize("7xl"),
		fontWeight: 900,
		fontFamily: '"Orbitron"',
		letterSpacing: 1,
		lineHeight: "110%",
		wrap: "wrap",
	},
	heading2: {
		fill: "#7ABCFB", // azul céu
		fontSize: twFontSize("3xl"),
		fontWeight: 600,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.8,
		lineHeight: "120%",
	},
	subtitle: {
		fill: "#A0A0A0",
		fontSize: twFontSize("2xl"),
		fontWeight: 500,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.5,
		lineHeight: "130%",
	},
	accent: {
		fill: "#D9A066", // madeira
		fontSize: twFontSize("lg"),
		fontWeight: 700,
		fontFamily: '"Orbitron"',
		letterSpacing: 0.5,
		lineHeight: "120%",
	},
	code: {
		fill: "#A0A0A0", // pedra
		fontSize: twFontSize("base"),
		fontWeight: 500,
		fontFamily: '"Fira Code"',
		letterSpacing: 0.2,
		lineHeight: "140%",
	},
} satisfies Record<string, Partial<TxtProps>>;
