import "./global.css";
import { makeProject } from "@motion-canvas/core";

import start from "./scenes/start?scene";
import opengl from "./scenes/opengl?scene";
import projection from "./scenes/projection?scene";

export default makeProject({
	scenes: [start, opengl, projection],
});
