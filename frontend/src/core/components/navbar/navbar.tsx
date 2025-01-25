import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";
import { SearchBar } from "./search-bar";

export const Navbar = () => {
  return (
    <nav className="flex items-center justify-between">
      <Logo />
      <SearchBar />
      <AuthSection />
    </nav>
  );
};
