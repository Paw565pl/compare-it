import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const deleteInactivePriceAlerts = async (accessToken: string) => {
  const { data } = await apiService.delete<undefined>(
    `/api/v1/price-alerts/inactive`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useDeleteInactivePriceAlerts = (accessToken: string) =>
  useMutation<undefined, AxiosError>({
    mutationKey: [...priceAlertsQueryKey, "deleteInactive"] as const,
    mutationFn: () => deleteInactivePriceAlerts(accessToken),
    onSettled: () => {
      const queryClient = getQueryClient();
      const queryKey = priceAlertsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
