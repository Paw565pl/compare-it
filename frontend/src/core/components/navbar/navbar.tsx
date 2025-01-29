import { AuthSection } from "@/core/components/navbar/auth-section";
import { Logo } from "@/core/components/navbar/logo";
import { SearchBar } from "@/core/components/navbar/search-bar";
import Link from "next/link";
import { Suspense } from "react";

export const Navbar = () => {
  return (
    <nav className="mb-8 flex flex-col items-center justify-between sm:flex-row">
      <Link href="/" className="hidden sm:flex">
        <Logo />
      </Link>
      <div className="flex w-full justify-between sm:hidden">
        <div className="flex items-center">
          <Logo />
        </div>
        <div className="block sm:hidden">
          <AuthSection />
        </div>
      </div>
      <div className="mt-4 w-full p-4 sm:mt-0 sm:p-0">
        <div className="mt-4 w-full p-4 sm:mt-0 sm:p-0">
          <Suspense>
            <SearchBar />
          </Suspense>
        </div>
      </div>
      <div className="hidden sm:block">
        <AuthSection />
      </div>
    </nav>
  );
};
