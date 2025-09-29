"use client";

import { Button } from "@/core/components/ui/button";
import { getClientEnv } from "@/core/libs/env/client-env";
import type { Route } from "next";
import { signOut } from "next-auth/react";
import { useRouter } from "next/navigation";

export const SignOutButton = () => {
  const { replace } = useRouter();

  const handleSignOut = async () => {
    const returnTo = window.location.origin;

    await signOut({
      redirect: true,
      redirectTo: "/",
    });

    const signOutUrl = `${getClientEnv("NEXT_PUBLIC_AUTH_AUTH0_ISSUER")}/v2/logout?federated&client_id=${getClientEnv("NEXT_PUBLIC_AUTH_AUTH0_ID")}&returnTo=${encodeURIComponent(returnTo)}`;

    replace(signOutUrl as Route);
  };

  return (
    <div className="border-primary border-b-2 px-2 py-4 sm:p-4">
      <Button variant="primary" onClick={handleSignOut} className="text-md">
        WYLOGUJ SIĘ
      </Button>
    </div>
  );
};
