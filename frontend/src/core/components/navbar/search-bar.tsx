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
        className="w-full border-2 border-r-0 border-gray-100 p-2 text-sm focus:outline-none md:w-1/3"
        placeholder="Wyszukaj wymarzony produkt"
      />
      <Link href={`/produkt?name=${name}`}>
        <div className="flex items-center bg-secondary p-2 font-semibold text-white transition-colors duration-300 hover:bg-secondary">
          <Search className="text-lg" />
          <div className="ml-2">WYSZUKAJ</div>
        </div>
      </Link>
    </div>
  );
};

export { SearchBar };
