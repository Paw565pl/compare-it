"use client";
import { Search } from "lucide-react";
import { parseAsFloat, parseAsString, useQueryStates } from "nuqs";
import { useRef } from "react";

const SearchBar = () => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [, setProductFilters] = useQueryStates({
    category: parseAsString,
    minPrice: parseAsFloat,
    maxPrice: parseAsFloat,
    shop: parseAsString,
    name: parseAsString,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const name = inputRef.current?.value.trim();
    if (name) {
      setProductFilters({
        name,
        category: null,
        minPrice: null,
        maxPrice: null,
        shop: null,
      });
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex w-full items-center justify-center"
    >
      <input
        ref={inputRef}
        type="text"
        className="w-full p-2 focus:outline-none md:w-1/3"
        placeholder="Wyszukaj produkt"
      />
      <button
        type="submit"
        className="flex items-center bg-secondary p-2 font-semibold text-white transition-colors duration-300 hover:bg-hover"
      >
        <Search className="text-lg" />
        <div className="ml-2 hidden md:block">WYSZUKAJ</div>
      </button>
    </form>
  );
};

export { SearchBar };
