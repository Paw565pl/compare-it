import { Role } from "@/auth/types/role";
import escapeStringRegexp from "escape-string-regexp";

export interface ProtectedRoutePattern {
  path: string;
  pattern: RegExp;
  roles?: Role[];
}

export const createProtectedRoutePattern = {
  exact: (path: string, roles?: Role[]): ProtectedRoutePattern => ({
    path,
    pattern: new RegExp(`^${escapeStringRegexp(path)}$`),
    roles,
  }),

  withChildren: (path: string, roles?: Role[]): ProtectedRoutePattern => ({
    path,
    pattern: new RegExp(`^${escapeStringRegexp(path)}(/.*)?$`),
    roles,
  }),

  withParam: (
    path: string,
    param: string,
    roles?: Role[],
  ): ProtectedRoutePattern => ({
    path: `${path}/:${param}`,
    pattern: new RegExp(`^${escapeStringRegexp(path)}/[^/]+$`),
    roles,
  }),

  withParams: (
    path: string,
    params: string[],
    roles?: Role[],
  ): ProtectedRoutePattern => ({
    path: `${path}/${params.map((p) => `:${p}`).join("/")}`,
    pattern: new RegExp(
      `^${escapeStringRegexp(path)}${"/[^/]+".repeat(params.length)}$`,
    ),
    roles,
  }),
};
