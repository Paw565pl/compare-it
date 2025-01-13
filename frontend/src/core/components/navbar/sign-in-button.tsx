import { signIn } from "@/auth/config/auth-config";
import { Button } from "@/core/components/ui/button";

export const SignInButton = () => {
  return (
    <form
      action={async () => {
        "use server";
        await signIn("auth0");
      }}
    >
      <Button type="submit">Zaloguj się</Button>
    </form>
  );
};
