import { auth } from "@/auth/config/auth-config";
import { ProfileLink } from "@/core/components/navbar/profile-link";
import { SignInButton } from "@/core/components/navbar/sign-in-button";
import { SignOutButton } from "@/core/components/navbar/sign-out-button";

export const AuthSection = async () => {
  const session = await auth();

  if (!session) return <SignInButton />;

  return (
    <div className="flex items-center gap-2 sm:gap-4">
      <ProfileLink />
      <SignOutButton />
    </div>
  );
};
