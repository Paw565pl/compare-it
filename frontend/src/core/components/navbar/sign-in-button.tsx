"use client";

import { Button } from "@/core/components/ui/button";
import { signIn } from "next-auth/react";

export const SignInButton = () => {
  return (
    <div className="border-b-2 border-secondary px-2 py-4 sm:p-4">
      <Button
        variant="secondary"
        className="text-md sm:text-lg"
        onClick={() => signIn("auth0")}
      >
        ZALOGUJ SIĘ
      </Button>
    </div>
  );
};
