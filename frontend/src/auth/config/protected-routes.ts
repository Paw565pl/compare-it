import {
  createProtectedRoutePattern,
  ProtectedRoutePattern,
} from "@/auth/utils/create-protected-route-pattern";

export const protectedRoutes: ProtectedRoutePattern[] = [
  createProtectedRoutePattern.withChildren("/profil"),
] as const;
