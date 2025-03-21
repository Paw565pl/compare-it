"use client";

import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";
import { Button } from "@/core/components/ui/button";
import { H2 } from "@/core/components/ui/h2";

const CategoriesList = () => {
  const { data: categoriesList } = useFetchCategoriesList();
  const [filters, setFilters] = useQueryStates(productFiltersSearchParams);
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const handleCategoryChange = (category: string | null) => {
    setFilters({ category });
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  return (
    <div className="flex flex-col items-start">
      <H2>
        Kategorie
      </H2>
      <ul className="mt-2 w-full bg-white">
        <li
          className={`cursor-pointer transition-colors duration-200 ${
            filters.category === null
              ? "bg-hover text-white"
              : "hover:bg-hover hover:text-white"
          }`}
        >
          <Button
            variant="category"
            onClick={() => handleCategoryChange(null)}
          >
            <span>Wszystkie</span>
          </Button>
        </li>

        {categoriesList?.map((category, index) => {
          const isActive = filters.category === category;

          return (
            <li
              key={index}
              className={`cursor-pointer transition-colors duration-200 ${
                isActive
                  ? "bg-hover text-white"
                  : "hover:bg-hover hover:text-white"
              }`}
            >
              <Button
                variant="category"
                onClick={() => handleCategoryChange(category)}
              >
                <span>{category}</span>
              </Button>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export { CategoriesList };
