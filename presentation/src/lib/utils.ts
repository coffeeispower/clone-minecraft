import { Camera } from "@motion-canvas/2d";
import { createSignal, Reference } from "@motion-canvas/core";

export function fixCamera(camera: Reference<Camera>) {
	camera()
		.scene()
		.position(
			createSignal(() => {
				return camera().view().size().div(2).add(camera().position().mul(-1).mul(camera().zoom()));
			}),
		);

	camera().scene().scale(camera().zoom);

	camera().cache(false);
}
