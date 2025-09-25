"use client";

import { ProfileLink } from "@/core/components/navbar/profile-link";
import { SignInButton } from "@/core/components/navbar/sign-in-button";
import { SignOutButton } from "@/core/components/navbar/sign-out-button";
import { Skeleton } from "@/core/components/ui/skeleton";
import { useSession } from "next-auth/react";

export const AuthSection = () => {
  const { data: session, status } = useSession();

  if (status === "loading")
    return (
      <div className="border-primary border-b-2 px-2 py-4 sm:p-4">
        <Skeleton className="h-9 w-31" />
      </div>
    );

  if (!session) return <SignInButton />;

  return (
    <div className="flex items-center sm:gap-4">
      <ProfileLink />
      <SignOutButton />
    </div>
  );
};
