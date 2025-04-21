import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const deletePriceAlert = async (accessToken: string, priceAlertId: string) => {
  const { data } = await apiService.delete<undefined>(
    `/api/v1/price-alerts/${priceAlertId}`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useDeletePriceAlert = (
  accessToken: string,
  priceAlertId: string,
) =>
  useMutation<undefined, AxiosError<ErrorResponse>>({
    mutationKey: [...priceAlertsQueryKey, priceAlertId, "delete"] as const,
    mutationFn: () => deletePriceAlert(accessToken, priceAlertId),
    onSettled: () => {
      const queryClient = getQueryClient();
      const queryKey = priceAlertsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
