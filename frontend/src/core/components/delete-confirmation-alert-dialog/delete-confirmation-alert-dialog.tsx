"use client";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/core/components/ui/alert-dialog";
import { Button, buttonVariants } from "@/core/components/ui/button";
import { VariantProps } from "class-variance-authority";
import { Ban, Trash2 } from "lucide-react";
import { ReactNode } from "react";

interface DeleteConfirmationAlertDialogProps {
  alertDialogTriggerLabel?: string;
  alertDialogTriggerIcon?: ReactNode;
  alertDialogTriggerClassName?: string;
  alertDialogTriggerVariant?: VariantProps<typeof buttonVariants>["variant"];
  alertDialogTriggerSize?: VariantProps<typeof buttonVariants>["size"];
  handleDelete: () => void;
}

export const DeleteConfirmationAlertDialog = ({
  alertDialogTriggerLabel,
  alertDialogTriggerIcon,
  alertDialogTriggerClassName,
  alertDialogTriggerVariant = "destructive",
  alertDialogTriggerSize,
  handleDelete,
}: DeleteConfirmationAlertDialogProps) => {
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button
          variant={alertDialogTriggerVariant}
          size={alertDialogTriggerSize}
          className={alertDialogTriggerClassName}
        >
          {alertDialogTriggerIcon ?? <Trash2 />} {alertDialogTriggerLabel}
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Czy jesteś pewny?</AlertDialogTitle>
          <AlertDialogDescription>
            Tej akcji nie można cofnąć.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>
            <Ban /> Anuluj
          </AlertDialogCancel>
          <AlertDialogAction variant="destructive" onClick={handleDelete}>
            <Trash2 /> Usuń
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
