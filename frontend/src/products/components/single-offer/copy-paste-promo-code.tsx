"use client";

import { Button } from "@/core/components/ui/button";
import { Copy } from "lucide-react";
import { toast } from "sonner";

interface CopyPastePromoCodeProps {
  readonly promoCode: string | null;
}

export const CopyPastePromoCode = ({ promoCode }: CopyPastePromoCodeProps) => {
  if (!promoCode) return null;

  const handleCopyPromoCode = async () => {
    if (!navigator.clipboard) return;

    await navigator.clipboard.writeText(promoCode);
    toast.success("Kod promocyjny został skopiowany do schowka.");
  };

  return (
    <Button
      variant="invisible"
      size="noPadding"
      className="mb-1 gap-1 text-xs text-blue-700"
      onClick={handleCopyPromoCode}
    >
      <Copy className="size-3.5!" /> {promoCode}
    </Button>
  );
};
