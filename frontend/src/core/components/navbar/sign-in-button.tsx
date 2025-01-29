"use client";

import { Button } from "@/core/components/ui/button";
import { signIn } from "next-auth/react";

export const SignInButton = () => {
  return (
    <Button
      className="text-md cursor-pointer rounded-none bg-secondary hover:bg-hover sm:text-lg"
      onClick={() => signIn("auth0")}
    >
      ZALOGUJ SIĘ
    </Button>
  );
};
