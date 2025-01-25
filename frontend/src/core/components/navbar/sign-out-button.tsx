"use client";

import { Button } from "@/core/components/ui/button";
import clientEnv from "@/core/libs/env/client-env";
import { signOut } from "next-auth/react";
import { useRouter } from "next/navigation";

export const SignOutButton = () => {
  const router = useRouter();

  const handleSignOut = async () => {
    await signOut({
      redirect: true,
      redirectTo: "/",
    });

    const signOutUrl = `${clientEnv.NEXT_PUBLIC_AUTH_AUTH0_ISSUER}/v2/logout?federated&client_id=${clientEnv.NEXT_PUBLIC_AUTH_AUTH0_ID}`;
    router.replace(signOutUrl);
  };

  return;
  <Button
    onClick={() => handleSignOut()}
    className="cursor-pointer rounded-none bg-secondary text-lg hover:bg-hover"
  >
    Wyloguj się
  </Button>;
};
