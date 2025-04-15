import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const createPriceAlert = async (
  accessToken: string,
  priceAlertDto: PriceAlertDto,
) => {
  const { data } = await apiService.post<PriceAlertEntity>(
    "/api/v1/price-alerts",
    priceAlertDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useCreatePriceAlert = (accessToken: string) =>
  useMutation<PriceAlertEntity, AxiosError<ErrorResponse>, PriceAlertDto>({
    mutationKey: [...priceAlertsQueryKey, "create"] as const,
    mutationFn: (priceAlertDto) => createPriceAlert(accessToken, priceAlertDto),
    onSettled: () => {
      const queryClient = getQueryClient();
      const queryKey = priceAlertsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
