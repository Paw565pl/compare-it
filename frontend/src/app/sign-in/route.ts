import { signIn } from "@/auth/config/auth-config";
import { NextRequest } from "next/server";

export const GET = async ({ url }: NextRequest) => {
  const searchParams = new URL(url).searchParams;
  const redirectTo = searchParams.get("redirectTo") ?? undefined;

  return await signIn("auth0", { redirectTo });
};
