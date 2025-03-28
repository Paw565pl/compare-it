"use client";

import { Button } from "@/core/components/ui/button";
import { signIn } from "next-auth/react";

export const SignInButton = () => {
  return (
    <div className="border-primary border-b-2 px-2 py-4 sm:p-4">
      <Button
        variant="primary"
        className="text-md"
        onClick={() => signIn("auth0")}
      >
        ZALOGUJ SIĘ
      </Button>
    </div>
  );
};
