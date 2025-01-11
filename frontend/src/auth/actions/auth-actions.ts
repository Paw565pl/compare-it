"use server";

import { signIn } from "@/auth/config/auth-config";

export const signInAction = async () => await signIn("auth0");
