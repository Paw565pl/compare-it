"use client";

import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

const CategoriesList = () => {
  const { data: categoriesList } = useFetchCategoriesList();
  const [filters, setFilters] = useQueryStates(productFiltersSearchParams);
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const handleCategoryChange = (category: string | null) => {
    setFilters({
      name: null,
      category,
      minPrice: null,
      maxPrice: null,
      shop: null,
    });
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  return (
    <div className="flex flex-col items-start">
      <h2 className="text-secondary mb-1 ml-4 text-2xl font-bold sm:ml-0">
        Kategorie
      </h2>
      <ul className="mt-2 w-full bg-white">
        <li
          className={`cursor-pointer transition-colors duration-200 ${
            filters.category === null
              ? "bg-hover text-white"
              : "hover:bg-hover hover:text-white"
          }`}
        >
          <button
            onClick={() => handleCategoryChange(null)}
            className="flex w-full justify-start px-4 py-2"
          >
            <span>Wszystkie</span>
          </button>
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
              <button
                onClick={() => handleCategoryChange(category)}
                className="flex w-full justify-start px-4 py-2"
              >
                <span>{category}</span>
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export { CategoriesList };
