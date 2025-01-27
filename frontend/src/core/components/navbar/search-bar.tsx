"use client";
import { Search } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

const SearchBar = () => {
  const [name, setName] = useState<string>("");

  return (
    <div className="flex w-full items-center justify-center">
      <input
        type="text"
        onChange={(e) => setName(e.target.value)}
        className="w-full p-2 focus:outline-none md:w-1/3"
        placeholder="Wyszukaj produkt"
      />
      <Link href={`/produkty/${name}`}>
        <div className="flex items-center bg-secondary p-2 font-semibold text-white transition-colors duration-300 hover:bg-hover">
          <Search className="text-lg" />
          <div className="ml-2 hidden md:block">WYSZUKAJ</div>
        </div>
      </Link>
    </div>
  );
};

export { SearchBar };
