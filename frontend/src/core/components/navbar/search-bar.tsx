"use client";

import { Button } from "@/core/components/ui/button";
import { Input } from "@/core/components/ui/input";
import { productFiltersSearchParams } from "@/products/search-params/product-search-params";
import { Search } from "lucide-react";
import { useRouter } from "next/navigation";
import { useQueryStates } from "nuqs";
import { FormEvent, useEffect, useRef } from "react";

export const SearchBar = () => {
  const { push } = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);

  const [productFilters, setProductFilters] = useQueryStates(
    productFiltersSearchParams,
  );

  useEffect(() => {
    const searchElement = inputRef.current;
    if (!searchElement) return;

    const name = productFilters.name?.trim();

    if (!name) searchElement.value = "";
    else searchElement.value = name;
  }, [productFilters.name]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const name = inputRef.current?.value.trim();

    if (!name) {
      setProductFilters({ name: null });
      return;
    }

    const paramsValues: Pick<
      Record<keyof typeof productFilters, string>,
      "name"
    > = { name };
    const params = new URLSearchParams(paramsValues);

    const url = `/produkty?${params.toString()}`;
    push(url);
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
        className="w-full bg-white md:w-1/3"
        placeholder="Wyszukaj produkt"
      />
      <Button variant="search" type="submit" aria-label="Wyszukaj produkty">
        <Search className="text-lg" />
        <span className="hidden md:block">WYSZUKAJ</span>
      </Button>
    </form>
  );
};
