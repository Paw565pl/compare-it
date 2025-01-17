import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";

export const Navbar = () => {
  return (
    <nav className="mb-8 flex items-center justify-between border-b-2 border-b-black px-4 py-6">
      <Logo />
      <AuthSection />
    </nav>
  );
};
