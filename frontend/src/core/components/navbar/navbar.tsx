import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";
import { SearchBar } from "@/core/components/navbar/search-bar";
import Link from "next/link";
import { Suspense } from "react";

export const Navbar = () => {
  return (
    <nav className="mb-6 flex flex-col items-center justify-between sm:mb-8 sm:flex-row">
      {/* Mobile View Link */}
      <Link href="/" className="hidden sm:flex">
        <Logo />
      </Link>
      <div className="flex w-full justify-between sm:hidden">
        {/* Desktop View Link */}
        <Link href="/" className="flex items-center">
          <Logo />
        </Link>
        {/* Desktop View Auth Section */}
        <div className="block sm:hidden">
          <AuthSection />
        </div>
      </div>
      <div className="w-full p-4 pb-0 sm:mt-0 sm:p-0">
        <Suspense>
          <SearchBar />
        </Suspense>
      </div>
      {/* Mobile View Auth Section */}
      <div className="hidden pl-2 sm:block">
        <AuthSection />
      </div>
    </nav>
  );
};
