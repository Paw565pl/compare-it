import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";
import { SearchBar } from "@/core/components/navbar/search-bar";
import Link from "next/link";
import { Suspense } from "react";

export const Navbar = () => {
  return (
    <nav className="mb-6 flex flex-col items-center justify-between sm:mb-8 sm:flex-row">
      <Link href="/" className="hidden sm:flex">
        <Logo />
      </Link>
      <div className="flex w-full justify-between sm:hidden">
        <Link href="/" className="flex items-center">
          <Logo />
        </Link>
        <div className="block sm:hidden">
          <AuthSection />
        </div>
      </div>
      <div className="w-full p-4 pb-0 sm:mt-0 sm:p-0">
        <Suspense>
          <SearchBar />
        </Suspense>
      </div>
      <div className="hidden pl-2 sm:block">
        <AuthSection />
      </div>
    </nav>
  );
};
