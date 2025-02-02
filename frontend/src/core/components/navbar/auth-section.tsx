"use client";

import { ProfileLink } from "@/core/components/navbar/profile-link";
import { SignInButton } from "@/core/components/navbar/sign-in-button";
import { SignOutButton } from "@/core/components/navbar/sign-out-button";
import { useSession } from "next-auth/react";

export const AuthSection = () => {
  const { data: session, status } = useSession();

  if (status === "loading") return null;
  if (!session) return <SignInButton />;

  return (
    <div className="flex items-center sm:gap-4">
      <ProfileLink />
      <SignOutButton />
    </div>
  );
};
