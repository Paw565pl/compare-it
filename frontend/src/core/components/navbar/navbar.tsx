import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";
import { SearchBar } from "./search-bar";

export const Navbar = () => {
  return (
    <nav className="mb-8 flex flex-col items-center justify-between sm:flex-row">
      <div className="flex w-full justify-between">
        <div className="flex items-center">
          <Logo />
        </div>
        <div className="block sm:hidden">
          <AuthSection />
        </div>
      </div>
      <div className="mt-4 w-full p-4 sm:mt-0 sm:p-0">
        <SearchBar />
      </div>
      <div className="hidden sm:block">
        <AuthSection />
      </div>
    </nav>
  );
};
