"use client";

import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { Search } from "lucide-react";
import { useQueryStates } from "nuqs";
import { FormEvent, useRef } from "react";

const SearchBar = () => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [, setProductFilters] = useQueryStates(productFiltersSearchParams);
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const handleSubmit = (e: FormEvent) => {
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

      setPagination((prev) => ({ ...prev, page: 0 }));
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
        className="w-full p-2 focus:outline-hidden md:w-1/3"
        placeholder="Wyszukaj produkt"
      />
      <button
        type="submit"
        className="flex items-center bg-secondary p-2 font-medium text-white transition-colors duration-300 hover:bg-hover"
      >
        <Search className="text-lg" />
        <div className="ml-2 hidden md:block">WYSZUKAJ</div>
      </button>
    </form>
  );
};

export { SearchBar };
