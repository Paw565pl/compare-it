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
import { Button } from "@/core/components/ui/button";
import { Ban, Trash2 } from "lucide-react";

interface DeleteConfirmationAlertDialogProps {
  alertDialogTriggerLabel?: string;
  alertDialogTriggerClassName?: string;
  handleDelete: () => void;
}

export const DeleteConfirmationAlertDialog = ({
  alertDialogTriggerLabel,
  alertDialogTriggerClassName,
  handleDelete,
}: DeleteConfirmationAlertDialogProps) => {
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="destructive" className={alertDialogTriggerClassName}>
          <Trash2 /> {alertDialogTriggerLabel || ""}
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
