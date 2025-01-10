"use server";

import { signIn, signOut } from "@/auth/config/auth-config";

export const signInAction = async () => await signIn("auth0");

export const signOutAction = async () =>
  await signOut({
    redirect: true,
    redirectTo: "/",
  });
