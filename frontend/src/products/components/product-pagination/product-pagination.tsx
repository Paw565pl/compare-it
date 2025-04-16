"use client";

import { Button } from "@/core/components/ui/button";
import { productPaginationSearchParams } from "@/products/search-params/product-search-params";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useQueryStates } from "nuqs";

interface ProductPaginationProps {
  readonly hasNextPage: boolean;
}

export const ProductPagination = ({ hasNextPage }: ProductPaginationProps) => {
  const [productPagination, setProductPagination] = useQueryStates(
    productPaginationSearchParams,
    { scroll: true },
  );

  return (
    <div className="mt-6 flex items-center justify-between">
      <Button
        variant="pagination"
        onClick={() =>
          setProductPagination((prev) => ({
            ...prev,
            page: Math.max(prev.page - 1, 1),
          }))
        }
        disabled={productPagination.page === 1}
      >
        <ChevronLeft />
      </Button>

      <span className="text-gray-700">Strona {productPagination.page}</span>

      <Button
        variant="pagination"
        onClick={() =>
          setProductPagination((prev) => ({ ...prev, page: prev.page + 1 }))
        }
        disabled={!hasNextPage}
      >
        <ChevronRight />
      </Button>
    </div>
  );
};
