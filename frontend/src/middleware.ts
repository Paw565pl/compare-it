export { auth as middleware } from "@/auth/config/auth-config";

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
};
