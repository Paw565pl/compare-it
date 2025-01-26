"use client";

import { Button } from "@/core/components/ui/button";
import clientEnv from "@/core/libs/env/client-env";
import { AlignJustify } from "lucide-react";
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

  return (
    <div className="border-b-2 border-secondary p-4">
      <Button
        onClick={() => handleSignOut()}
        className="cursor-pointer rounded-none bg-secondary text-sm hover:bg-hover sm:text-lg"
      >
        <div className="hidden md:block">WYLOGUJ SIĘ</div>
        <AlignJustify className="block md:hidden" />
      </Button>
    </div>
  );
};
