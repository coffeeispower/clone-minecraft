import { Img, Layout, makeScene2D, Rect, Txt } from "@motion-canvas/2d";
import { beginSlide, createRef, Direction, finishScene, slideTransition, waitFor, waitTransition } from "@motion-canvas/core";
import { textStyles } from "../lib/textStyles";
import { twFontSize, twSize } from "../lib/standardStyles";
import screenshotSrc from "../assets/start/screenshot.png";
import avatarSrc from "../assets/start/avatar.png";
import javaLogoSvg from "../assets/java.svg";
export default makeScene2D(function* (view) {
	const contentRef = createRef<Rect>();
	const subtitleRef = createRef<Txt>();
	view.add(
		<Rect layout width={"100%"} height={"100%"} ref={contentRef}>
			<Rect layout justifyContent={"center"} width={"100%"} height={"100%"}>
				<Rect layout alignItems={"start"} justifyContent={"center"} gap={twSize("8")} padding={twSize("6")} direction={"column"} height={"100%"}>
					<Rect
						smoothCorners
						padding={[twSize("4"), twSize("6")]}
						lineWidth={4}
						stroke={"#0e3642"}
						radius={twSize("10")}
						gap={twSize("4")}
						alignItems={"center"}
					>
						<Img src={javaLogoSvg} width={twSize(6)}/>
						<Txt {...textStyles.normal} fontWeight={800}>
							Projeto de Programação em Java
						</Txt>
					</Rect>
					<Rect layout direction={"column"} gap={twSize("6")}>
						<Txt {...textStyles.heading1}>{"Mini Clone de\nMinecraft"}</Txt>
						<Txt {...textStyles.subtitle} ref={subtitleRef}>
							{"com LWJGL / OpenGL"}
						</Txt>
					</Rect>
					<Rect marginTop={twSize("10")}>
						<Rect layout gap={twSize("4")}>
							<Img src={avatarSrc} width={100} radius={18} smoothCorners />
							<Rect layout direction={"column"} height={"100%"} justifyContent={"center"} gap={twSize(1)}>
								<Txt.b {...textStyles.normal}>Tiago Dinis</Txt.b>
								<Txt.b {...textStyles.dimmed}>Aluno do 3º IS</Txt.b>
							</Rect>
						</Rect>
					</Rect>
				</Rect>
			</Rect>
			<Rect layout justifyContent={"center"} alignItems={"center"} width={"100%"} height={"100%"} padding={twSize("6")}>
				<Img src={screenshotSrc} shadowColor={"#0000005F"} shadowBlur={70} shadowOffset={0} />
			</Rect>
		</Rect>,
	);

	yield* beginSlide("início");
	finishScene();
	subtitleRef().text("com LWJGL /");
	yield* contentRef().opacity(0.0, 0.6);
});
