"use client";

import { Button } from "@/core/components/ui/button";
import { H2 } from "@/core/components/ui/h2";
import { Ul } from "@/core/components/ui/ul";
import { cn } from "@/core/utils/cn";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import { productFiltersSearchParams } from "@/products/search-params/product-search-params";
import { useRouter } from "next/navigation";
import { useQueryStates } from "nuqs";

import { categoryDisplayNameMap } from "@/products/entities/category-entity";
import type { Route } from "next";

export const CategoriesList = () => {
  const { push } = useRouter();
  const { data: categoriesList } = useFetchCategoriesList();

  const [productFilters] = useQueryStates(productFiltersSearchParams);

  const handleCategoryChange = (category: string | null) => {
    const paramsValues: Partial<
      Pick<Record<keyof typeof productFilters, string>, "name" | "category">
    > = {};

    if (productFilters.name) paramsValues.name = productFilters.name;
    if (category) paramsValues.category = category;

    const params = new URLSearchParams(paramsValues);

    const url = `/produkty?${params.toString()}`;
    push(url as Route);
  };

  return (
    <div className="flex flex-col items-start">
      <H2 className="lg:mt-1">Kategorie</H2>

      <Ul>
        <li
          className={cn(
            "cursor-pointer transition-colors duration-200",
            productFilters.category === null
              ? "bg-hover text-white"
              : "hover:bg-hover hover:text-white",
          )}
        >
          <Button variant="category" onClick={() => handleCategoryChange(null)}>
            <span>Wszystkie</span>
          </Button>
        </li>

        {categoriesList?.map((category, index) => {
          const isActive = productFilters.category === category;

          return (
            <li
              key={index}
              className={cn(
                "cursor-pointer transition-colors duration-200",
                isActive
                  ? "bg-hover text-white"
                  : "hover:bg-hover hover:text-white",
              )}
            >
              <Button
                variant="category"
                onClick={() => handleCategoryChange(category)}
              >
                <span>{categoryDisplayNameMap[category]}</span>
              </Button>
            </li>
          );
        })}
      </Ul>
    </div>
  );
};
