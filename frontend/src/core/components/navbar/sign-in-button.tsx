import { signIn } from "@/auth/config/auth-config";
import { Button } from "@/core/components/ui/button";

export const SignInButton = () => {
  return (
    <form
      className="border-b-2 border-secondary p-4"
      action={async () => {
        "use server";
        await signIn("auth0");
      }}
    >
      <Button
        className="text-md cursor-pointer rounded-none bg-secondary hover:bg-hover sm:text-lg"
        type="submit"
      >
        ZALOGUJ SIĘ
      </Button>
    </form>
  );
};
