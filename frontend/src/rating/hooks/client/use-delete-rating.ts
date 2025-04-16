/* eslint-disable @typescript-eslint/no-invalid-void-type */
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const deleteRating = async (
  accessToken: string,
  productId: string,
  commentId: string,
) => {
  const { data } = await apiService.delete<void>(
    `/api/v1/products/${productId}/comments/${commentId}/rate`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useDeleteRating = (
  accessToken: string,
  productId: string,
  commentId: string,
) =>
  useMutation<void, AxiosError<ErrorResponse>, void>({
    mutationKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "rating",
      "delete",
    ] as const,
    mutationFn: () => deleteRating(accessToken, productId, commentId),
    onSettled: () => {
      const queryClient = getQueryClient();
      const queryKey = [
        ...productsQueryKey,
        productId,
        ...commentsQueryKey,
      ] as const;

      queryClient.invalidateQueries({ queryKey });
    },
  });
