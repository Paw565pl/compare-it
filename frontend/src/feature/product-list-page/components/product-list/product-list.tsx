"use client";
import { ProductFiltersDto } from "@/products/dtos/product-filters-dto";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import { useState } from "react";

const ProductList = () => {
  const [filters, setFilters] = useState<ProductFiltersDto>({
    category: "Karty graficzne",
  });

  const [pagination, setPagination] = useState({
    page: 1,
    limit: 10,
  });

  const { data } = useFetchProductPage(filters);

  console.log(data);
  return (
    <div className="w-full rounded-lg bg-white p-2">
      Product List
      <ul></ul>
    </div>
  );
};

export { ProductList };
