"use client";

import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/core/components/ui/avatar";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { H1 } from "@/core/components/ui/h1";
import { useSession } from "next-auth/react";

export const UserDetailsCard = () => {
  const { data: session } = useSession();
  if (!session?.user) return null;

  const user = session.user;

  return (
    <>
      <H1 className="mb-4 sm:mb-8">Twój profil</H1>

      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-lg font-semibold">
            Szczegóły konta
          </CardTitle>
        </CardHeader>
        <CardContent className="flex items-center gap-6">
          <Avatar className="h-20 w-20">
            <AvatarImage src={user.picture} alt={user.username} />
            <AvatarFallback>
              {user.username.slice(0, 2).toUpperCase()}
            </AvatarFallback>
          </Avatar>

          <div className="space-y-1.5">
            <div>
              <h3 className="text-lg font-medium">{user.username}</h3>
              <p className="text-sm text-muted-foreground">{user.email}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </>
  );
};
