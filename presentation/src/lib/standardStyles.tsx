import { RectProps } from "@motion-canvas/2d";

type TailwindSpacingNumbers =
	| 0
	| 0.5
	| 1
	| 1.5
	| 2
	| 2.5
	| 3
	| 3.5
	| 4
	| 5
	| 6
	| 7
	| 8
	| 9
	| 10
	| 11
	| 12
	| 14
	| 16
	| 20
	| 24
	| 28
	| 32
	| 36
	| 40
	| 44
	| 48
	| 52
	| 56
	| 60
	| 64
	| 72
	| 80
	| 96;
const pxPerRem = Math.floor(16 * 1.5); // 1rem = 16px

/**
 * Todos os valores válidos de espaçamento no tema padrão do Tailwind.
 */
type TailwindSpacing = TailwindSpacingNumbers | `${TailwindSpacingNumbers}`;
/**
 * Converte uma unidade de espaçamento Tailwind (ex: 4) em pixels.
 * Baseado no tema padrão do Tailwind (1 = 0.25rem e 1rem = 16px).
 */
export function twSize(value: TailwindSpacing): number {
	const n = typeof value === "string" ? parseFloat(value) : value;
	const remPerUnit = 0.25; // Tailwind base: 1 -> 0.25rem
	return n * remPerUnit * pxPerRem;
}

/**
 * Todos os tamanhos de fonte padrão do Tailwind.
 */
export type TailwindFontSize = "xs" | "sm" | "base" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl" | "6xl" | "7xl" | "8xl" | "9xl";

/**
 * Converte um tamanho de fonte Tailwind em pixels.
 * Baseado no tema padrão (1rem = 16px).
 */
export function twFontSize(size: TailwindFontSize): number {
	const map: Record<TailwindFontSize, number> = {
		xs: 0.75,
		sm: 0.875,
		base: 1,
		lg: 1.125,
		xl: 1.25,
		"2xl": 1.5,
		"3xl": 1.875,
		"4xl": 2.25,
		"5xl": 3,
		"6xl": 3.75,
		"7xl": 4.5,
		"8xl": 6,
		"9xl": 8,
	};

	return map[size] * pxPerRem;
}
export const baseColors = {
	cardBg: "#061816",
	badgeBg: "#68a5d7",
};
export const cardStyle: Partial<RectProps> = {
	fill: baseColors.cardBg,
	padding: twSize("6"),
	smoothCorners: true,
	radius: twSize("3"),
};

export const badgeStyle: Partial<RectProps> = {
	fill: baseColors.badgeBg,
	padding: twSize("3"),
	smoothCorners: true,
	radius: twSize("3"),
};
