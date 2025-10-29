// Reexport the native module. On web, it will be resolved to ExpoAudioRouteModule.web.ts
// and on native platforms to ExpoAudioRouteModule.ts
export * from "./src/ExpoAudioRoute.types";
export { default } from "./src/ExpoAudioRouteModule";
