"use client";

import { Button } from "@/core/components/ui/button";
import { productPaginationSearchParams } from "@/products/search-params/product-search-params";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useQueryStates } from "nuqs";

interface ProductPaginationProps {
  readonly hasNextPage: boolean;
}

export const ProductPagination = ({ hasNextPage }: ProductPaginationProps) => {
  const [pagination, setPagination] = useQueryStates(
    productPaginationSearchParams,
  );

  return (
    <div className="mt-6 flex items-center justify-between">
      <Button
        variant="pagination"
        onClick={() =>
          setPagination((prev) => ({
            ...prev,
            page: Math.max(prev.page - 1, 1),
          }))
        }
        disabled={pagination.page === 1}
      >
        <ChevronLeft />
      </Button>

      <span className="text-gray-700">Strona {pagination.page}</span>

      <Button
        variant="pagination"
        onClick={() =>
          setPagination((prev) => ({ ...prev, page: prev.page + 1 }))
        }
        disabled={!hasNextPage}
      >
        <ChevronRight />
      </Button>
    </div>
  );
};
