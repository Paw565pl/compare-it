"use client";

import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { Search } from "lucide-react";
import { useQueryStates } from "nuqs";
import { FormEvent, useEffect, useRef } from "react";
import { Input } from "../ui/input";

const SearchBar = () => {
  const inputRef = useRef<HTMLInputElement>(null);

  const [productFilters, setProductFilters] = useQueryStates(
    productFiltersSearchParams,
  );
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  useEffect(() => {
    const searchElement = inputRef.current;
    if (!searchElement) return;

    const name = productFilters.name?.trim();

    if (!name) searchElement.value = "";
    else searchElement.value = name;
  }, [productFilters.name]);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const name = inputRef.current?.value.trim();

    if (!name) {
      setProductFilters({ name: null });
    } else {
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
      <Input
        type="text"
        ref={inputRef}
        defaultValue={productFilters.name?.trim() || ""}
        className="w-full bg-white p-2 focus:outline-hidden md:w-1/3"
        placeholder="Wyszukaj produkt"
      />
      <button
        type="submit"
        className="bg-primary hover:bg-hover flex items-center p-2 font-medium text-white transition-colors duration-300"
      >
        <Search className="text-lg" />
        <div className="ml-2 hidden md:block">WYSZUKAJ</div>
      </button>
    </form>
  );
};

export { SearchBar };
