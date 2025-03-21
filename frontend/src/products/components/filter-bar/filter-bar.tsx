"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";
import { ChangeEvent, useState } from "react";
import { Input } from "@/core/components/ui/input";
import { H2 } from "@/core/components/ui/h2";
import { H3 } from "@/core/components/ui/h3";
import { Button } from "@/core/components/ui/button";
import { Label } from "@/core/components/ui/label";

const FilterBar = () => {
  const { data: shopList } = useFetchShopsList();
  const [, setProductFilters] = useQueryStates(productFiltersSearchParams);
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const [tempProductFilters, setTempProductFilters] = useState({
    minPrice: null,
    maxPrice: null,
    shop: ["Morele.net", "RTV Euro AGD"],
  });

  const handlePriceChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setTempProductFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleShopChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { value, checked } = e.target;

    setTempProductFilters((prev) => {
      const updatedShops = checked
        ? [...prev.shop, value]
        : prev.shop.filter((shop) => shop !== value);

      return {
        ...prev,
        shop: updatedShops,
      };
    });
  };

  const applyFilters = () => {
    const filtersWithShops = {
      ...tempProductFilters,
      shop: tempProductFilters.shop.join(","),
    };

    setProductFilters((prevFilters) => ({
      ...prevFilters,
      ...filtersWithShops,
    }));

    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  return (
    <>
      <H2 className="mt-6 mb-1 ml-4 sm:ml-0">
        Filtry
      </H2>
      <div className="border-background mt-2 mb-4 flex flex-col items-start border bg-white">
        <H3 className="text-primary mb-2 pt-2 pl-4 text-xl font-bold">
          Sklepy
        </H3>
        <ul className="w-full">
          {shopList
            ?.filter((shop) => shop !== "Media Expert")
            .map((shop, index) => (
              <li
                key={index}
                className="hover:bg-hover cursor-pointer px-4 py-2 transition-colors duration-200 hover:text-white sm:text-lg"
              >
                <Label className="flex items-center space-x-2 hover:cursor-pointer">
                  <Input
                    type="checkbox"
                    name="shop"
                    value={shop}
                    checked={tempProductFilters.shop.includes(shop)}
                    onChange={handleShopChange}
                    className="cursor-pointer"
                  />
                  <span>{shop}</span>
                </Label>
              </li>
            ))}
        </ul>
        <H3 className="text-primary pt-2 pl-4 text-xl font-bold">Cena</H3>
        <div className="flex w-full flex-col justify-start">
          <div className="mb-4 w-full space-y-1 p-4">
            <Input
              type="text"
              name="minPrice"
              onChange={handlePriceChange}
              id="minPrice"
              placeholder="od"
              className="bg-background w-full p-2 text-sm focus:outline-hidden"
            />
            <Input
              type="text"
              name="maxPrice"
              onChange={handlePriceChange}
              id="maxPrice"
              placeholder="do"
              className="bg-background w-full p-2 text-sm focus:outline-hidden"
            />
          </div>

          <Button
            variant="primary"
            onClick={applyFilters}
          >
            FILTRUJ
          </Button>
        </div>
      </div>
    </>
  );
};

export { FilterBar };
