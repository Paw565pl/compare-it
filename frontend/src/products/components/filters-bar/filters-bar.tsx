"use client";

import { Button } from "@/core/components/ui/button";
import { Checkbox } from "@/core/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
} from "@/core/components/ui/form";
import { H2 } from "@/core/components/ui/h2";
import { H3 } from "@/core/components/ui/h3";
import { Input } from "@/core/components/ui/input";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";
import { useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";

interface ProductFiltersFields {
  minPrice: string;
  maxPrice: string;
  shop: string[];
}

export const FiltersBar = () => {
  const { data: shopList } = useFetchShopsList();

  const [productFilters, setProductFilters] = useQueryStates(
    productFiltersSearchParams,
  );
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const defaultValues = useMemo(
    () =>
      ({
        minPrice: productFilters.minPrice?.toString() ?? "",
        maxPrice: productFilters.maxPrice?.toString() ?? "",
        shop: productFilters.shop?.split(",") ?? shopList ?? [],
      }) as const,
    [
      productFilters.maxPrice,
      productFilters.minPrice,
      productFilters.shop,
      shopList,
    ],
  );
  const form = useForm<ProductFiltersFields>({
    defaultValues,
  });

  useEffect(() => form.reset(defaultValues), [form, defaultValues]);

  const handleSubmit = ({ minPrice, maxPrice, shop }: ProductFiltersFields) => {
    const parsedShop =
      shop.length === 0 || shop.length === shopList?.length
        ? null
        : shop.join(",");
    const parsedFilters = {
      shop: parsedShop,
      minPrice: Number(minPrice) || null,
      maxPrice: Number(maxPrice) || null,
    } as const;

    setProductFilters((prevFilters) => ({
      ...prevFilters,
      ...parsedFilters,
    }));
    setPagination((prev) => ({ ...prev, page: 1 }));
  };

  return (
    <>
      <H2 className="mt-2">Filtry</H2>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)}>
          <div className="mt-2 bg-white py-4">
            <H3>Sklepy</H3>

            <FormField
              control={form.control}
              name="shop"
              render={() => (
                <FormItem className="block w-full">
                  {shopList?.map((shop, index) => (
                    <FormField
                      control={form.control}
                      name="shop"
                      key={index}
                      render={({ field }) => (
                        <FormItem key={index}>
                          <FormLabel className="hover:bg-hover flex w-full cursor-pointer items-center px-4 py-3 transition-colors duration-200 hover:text-white">
                            <FormControl>
                              <Checkbox
                                checked={field.value?.includes(shop)}
                                onCheckedChange={(checked) =>
                                  checked
                                    ? field.onChange([...field.value, shop])
                                    : field.onChange(
                                        field.value?.filter(
                                          (value) => value !== shop,
                                        ),
                                      )
                                }
                                className="cursor-pointer"
                              />
                            </FormControl>
                            <FormLabel className="cursor-pointer sm:text-lg">
                              {shop}
                            </FormLabel>
                          </FormLabel>
                        </FormItem>
                      )}
                    />
                  ))}
                </FormItem>
              )}
            />

            <H3>Cena</H3>

            <div className="w-full space-y-1 px-4">
              <FormField
                control={form.control}
                name="minPrice"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <Input
                        {...field}
                        onChange={(e) => {
                          const value = e.target.value.replace(/[^0-9]/g, "");
                          field.onChange(value);
                        }}
                        type="text"
                        inputMode="numeric"
                        placeholder="od"
                        className="bg-background w-full border-0 p-2 text-sm focus:outline-hidden"
                      />
                    </FormControl>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="maxPrice"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <Input
                        {...field}
                        onChange={(e) => {
                          const value = e.target.value.replace(/[^0-9]/g, "");
                          field.onChange(value);
                        }}
                        type="text"
                        inputMode="numeric"
                        placeholder="do"
                        className="bg-background w-full border-0 p-2 text-sm focus:outline-hidden"
                      />
                    </FormControl>
                  </FormItem>
                )}
              />
            </div>
          </div>

          <Button type="submit" variant="filter">
            FILTRUJ
          </Button>
        </form>
      </Form>
    </>
  );
};
