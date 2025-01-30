"use client";

import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/core/components/ui/avatar";
import { useSession } from "next-auth/react";
import Link from "next/link";

export const ProfileLink = () => {
  const { data: session } = useSession();
  if (!session?.user) return null;

  const user = session.user;

  return (
    <Link href="/profil" className="flex items-center gap-2">
      <Avatar className="h-10 w-10">
        <AvatarImage src={user.picture} alt={user.username} />
        <AvatarFallback>
          {user.username.slice(0, 2).toUpperCase()}
        </AvatarFallback>
      </Avatar>

      <span className="hidden sm:block">{user.username}</span>
    </Link>
  );
};
