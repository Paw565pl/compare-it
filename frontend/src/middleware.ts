import { auth } from "@/auth/config/auth-config";
import { protectedRoutes } from "@/auth/config/protected-routes";
import { hasRequiredRole } from "@/auth/utils/has-required-role";
import { notFound } from "next/navigation";
import { NextResponse } from "next/server";

export const middleware = auth(async (req) => {
  const { nextUrl, auth } = req;
  const currentPathName = nextUrl.pathname;

  const isUserLoggedIn = !!auth;
  const protectedRoute = protectedRoutes.find((page) =>
    page.pattern.test(currentPathName),
  );

  // auth check
  if (protectedRoute && !isUserLoggedIn) {
    const signInUrl = nextUrl.clone();

    signInUrl.pathname = "/sign-in";
    signInUrl.searchParams.set("redirectTo", currentPathName);

    return NextResponse.redirect(signInUrl);
  }

  // role check
  const requiresRolesCheck = !!protectedRoute?.roles?.length;
  if (protectedRoute && isUserLoggedIn && requiresRolesCheck) {
    const hasRequiredRoles = protectedRoute.roles?.every((role) =>
      hasRequiredRole(auth, role),
    );
    if (!hasRequiredRoles) return notFound();
  }

  return NextResponse.next();
});

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
};
